using System.Collections.Generic;
using Pulumi;
using Kubernetes = Pulumi.Kubernetes;

return await Deployment.RunAsync(() =>
{
    var config = new Config();
    var kubernetesKubeconfig = config.Require("kubernetesKubeconfig");
    var k8SProvider = new Kubernetes.Provider("k8sProvider", new()
    {
        KubeConfig = kubernetesKubeconfig,
    });

    var awesomeSite = new Kubernetes.Core.V1.Namespace("awesome-site", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Name = "awesome-site",
        },
    }, new CustomResourceOptions
    {
        Provider = k8SProvider,
    });

    return new Dictionary<string, object?>
    {
        ["name"] = awesomeSite.Metadata.Apply(metadata => metadata?.Name),
    };
});
