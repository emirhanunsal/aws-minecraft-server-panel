import json
import os

import boto3


ec2 = boto3.client("ec2")
INSTANCE_ID = os.environ["INSTANCE_ID"]


def lambda_handler(event, context):
    action = get_action(event)

    if action == "start":
        return start_instance()
    if action == "stop":
        return stop_instance()
    if action == "status":
        return get_status()

    return create_response(400, {
        "message": "Invalid action",
        "allowedActions": ["start", "stop", "status"]
    })


def get_action(event):
    # Lambda Console test event
    if event.get("action"):
        return event["action"].lower()

    # API Gateway HTTP API event
    path = event.get("rawPath", "").rstrip("/")
    if path.endswith("/start"):
        return "start"
    if path.endswith("/stop"):
        return "stop"
    if path.endswith("/status"):
        return "status"
    return None


def start_instance():
    state = get_instance_state()
    if state in ["running", "pending"]:
        return create_response(200, {
            "state": state,
            "message": "Minecraft server is already running or starting"
        })
    if state not in ["stopped"]:
        return create_response(409, {
            "state": state,
            "message": f"Server cannot be started while state is {state}"
        })

    ec2.start_instances(InstanceIds=[INSTANCE_ID])
    return create_response(202, {
        "state": "pending",
        "message": "Minecraft server is starting"
    })


def stop_instance():
    state = get_instance_state()
    if state in ["stopped", "stopping"]:
        return create_response(200, {
            "state": state,
            "message": "Minecraft server is already stopped or stopping"
        })
    if state not in ["running"]:
        return create_response(409, {
            "state": state,
            "message": f"Server cannot be stopped while state is {state}"
        })

    ec2.stop_instances(InstanceIds=[INSTANCE_ID])
    return create_response(202, {
        "state": "stopping",
        "message": "Minecraft server is stopping"
    })


def get_status():
    state = get_instance_state()
    return create_response(200, {
        "state": state
    })


def get_instance_state():
    result = ec2.describe_instances(InstanceIds=[INSTANCE_ID])
    return (
        result["Reservations"][0]
        ["Instances"][0]
        ["State"]["Name"]
    )


def create_response(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(body)
    }
