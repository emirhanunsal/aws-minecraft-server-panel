# Minecraft Instance Controller Lambda

Python Lambda for idempotent `start`, `stop`, and `status` operations on one EC2 instance. Use a supported Python runtime and handler `lambda_function.lambda_handler`.

- `INSTANCE_ID` (required): target instance

Replace the placeholders in `iam-policy.json`, attach it to the execution role, and deploy `lambda_function.py` (Boto3 is provided by the runtime). Test with `{"action":"status"}`, `start`, and `stop`, then review CloudWatch Logs. Do not add static credentials or API Gateway.

```bash
python3 -m unittest discover lambda/minecraft-instance-controller/tests
```
