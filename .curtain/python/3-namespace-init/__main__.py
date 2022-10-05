import pulumi
import pulumi_kubernetes as kubernetes

config = pulumi.Config()
kubernetes_kubeconfig = config.require("kubernetesKubeconfig")
k8_s_provider = kubernetes.Provider("k8sProvider", kubeconfig=kubernetes_kubeconfig)
awesome_site = kubernetes.core.v1.Namespace("awesome-site", metadata=kubernetes.meta.v1.ObjectMetaArgs(
    name="awesome-site",
),
opts=pulumi.ResourceOptions(provider=k8_s_provider))
pulumi.export("name", awesome_site.metadata.name)
