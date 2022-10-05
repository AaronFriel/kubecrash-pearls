import * as pulumi from "@pulumi/pulumi";
import * as kubernetes from "@pulumi/kubernetes";

const k8SProvider = new kubernetes.Provider("k8sProvider", {enableServerSideApply: true});
const deployment = new kubernetes.apps.v1.DeploymentPatch("deployment", {
    metadata: {
        namespace: "awesome-site",
        name: "wordpress",
        annotations: {
            "pulumi.com/patchForce": "true",
        },
    },
    spec: {
        replicas: 3,
    },
}, {
    provider: k8SProvider,
});
export const status = deployment.status;
