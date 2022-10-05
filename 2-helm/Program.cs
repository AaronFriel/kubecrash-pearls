using System.Collections.Generic;
using Pulumi;
using Kubernetes = Pulumi.Kubernetes;

return await Deployment.RunAsync(() =>
{
  var ingressNginx = new Kubernetes.Helm.V3.Release("ingress-nginx", new()
  {
    Namespace = "admin-ingress-nginx",
    Chart = "ingress-nginx",
    RepositoryOpts = new Kubernetes.Types.Inputs.Helm.V3.RepositoryOptsArgs
    {
      Repo = "https://kubernetes.github.io/ingress-nginx",
    },
    Values = new Dictionary<string, object> { },
    CreateNamespace = true,
  });
});
