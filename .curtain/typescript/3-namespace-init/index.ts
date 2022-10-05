import * as pulumi from "@pulumi/pulumi";
import * as kubernetes from "@pulumi/kubernetes";

const config = new pulumi.Config();
const kubernetesKubeconfig = config.require("kubernetesKubeconfig");
const k8SProvider = new kubernetes.Provider("k8sProvider", {kubeconfig: kubernetesKubeconfig});
const awesomeSite = new kubernetes.core.v1.Namespace("awesome-site", {metadata: {
    name: "awesome-site",
}}, {
    provider: k8SProvider,
});
export const name = awesomeSite.metadata.apply(metadata => metadata?.name);
