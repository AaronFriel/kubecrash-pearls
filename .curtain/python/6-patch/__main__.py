import pulumi
import pulumi_kubernetes as kubernetes

k8_s_provider = kubernetes.Provider("k8sProvider", enable_server_side_apply=True)
deployment = kubernetes.apps.v1.DeploymentPatch("deployment",
    metadata=kubernetes.meta.v1.ObjectMetaPatchArgs(
        namespace="awesome-site",
        name="wordpress",
        annotations={
            "pulumi.com/patchForce": "true",
        },
    ),
    spec=kubernetes.apps.v1.DeploymentSpecPatchArgs(
        replicas=3,
    ),
    opts=pulumi.ResourceOptions(provider=k8_s_provider))
pulumi.export("status", deployment.status)
