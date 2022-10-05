using System.Collections.Generic;
using Pulumi;
using Kubernetes = Pulumi.Kubernetes;

return await Deployment.RunAsync(() =>
{
    var k8SProvider = new Kubernetes.Provider("k8sProvider", new()
    {
        EnableServerSideApply = true,
    });

    var deployment = new Kubernetes.Apps.V1.DeploymentPatch("deployment", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaPatchArgs
        {
            Namespace = "awesome-site",
            Name = "wordpress",
            Annotations =
            {
                { "pulumi.com/patchForce", "true" },
            },
        },
        Spec = new Kubernetes.Types.Inputs.Apps.V1.DeploymentSpecPatchArgs
        {
            Replicas = 3,
        },
    }, new CustomResourceOptions
    {
        Provider = k8SProvider,
    });

    return new Dictionary<string, object?>
    {
        ["status"] = deployment.Status,
    };
});
