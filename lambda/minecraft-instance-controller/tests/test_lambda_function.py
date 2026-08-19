import importlib
import json
import os
import sys
import types
import unittest
from unittest.mock import Mock, patch

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
os.environ.setdefault("INSTANCE_ID", "i-test")

fake_boto3 = types.ModuleType("boto3")
fake_boto3.client = Mock(return_value=Mock())
fake_exceptions = types.ModuleType("botocore.exceptions")
fake_exceptions.BotoCoreError = type("BotoCoreError", (Exception,), {})
fake_exceptions.ClientError = type("ClientError", (Exception,), {})
sys.modules.setdefault("boto3", fake_boto3)
sys.modules.setdefault("botocore", types.ModuleType("botocore"))
sys.modules.setdefault("botocore.exceptions", fake_exceptions)
handler = importlib.import_module("lambda_function")


class LambdaHandlerTest(unittest.TestCase):
    def setUp(self):
        self.ec2 = Mock()
        self.ec2.describe_instances.return_value = {
            "Reservations": [{"Instances": [{"State": {"Name": "stopped"}}]}]
        }
        self.ec2_patch = patch.object(handler, "ec2", self.ec2)
        self.ec2_patch.start()

    def tearDown(self):
        self.ec2_patch.stop()

    def invoke(self, action):
        response = handler.lambda_handler({"action": action}, None)
        return response, json.loads(response["body"])

    def set_state(self, state):
        self.ec2.describe_instances.return_value["Reservations"][0]["Instances"][0]["State"]["Name"] = state

    def test_status_returns_only_safe_fields(self):
        response, body = self.invoke("status")
        self.assertEqual(200, response["statusCode"])
        self.assertEqual({"state", "minecraftAddress"}, set(body))

    def test_start_stopped_instance(self):
        response, body = self.invoke("start")
        self.assertEqual((202, "pending"), (response["statusCode"], body["state"]))
        self.ec2.start_instances.assert_called_once_with(InstanceIds=["i-test"])

    def test_idempotent_start_and_stop(self):
        self.set_state("running")
        self.assertEqual(200, self.invoke("start")[0]["statusCode"])
        self.ec2.start_instances.assert_not_called()
        self.set_state("stopping")
        self.assertEqual(200, self.invoke("stop")[0]["statusCode"])
        self.ec2.stop_instances.assert_not_called()

    def test_stop_running_instance(self):
        self.set_state("running")
        response, body = self.invoke("stop")
        self.assertEqual((202, "stopping"), (response["statusCode"], body["state"]))
        self.ec2.stop_instances.assert_called_once_with(InstanceIds=["i-test"])

    def test_invalid_action_and_transition(self):
        self.assertEqual(400, self.invoke("reboot")[0]["statusCode"])
        self.set_state("pending")
        self.assertEqual(409, self.invoke("stop")[0]["statusCode"])

    def test_api_gateway_path_is_supported(self):
        response = handler.lambda_handler({"rawPath": "/status"}, None)
        self.assertEqual(200, response["statusCode"])


if __name__ == "__main__":
    unittest.main()
