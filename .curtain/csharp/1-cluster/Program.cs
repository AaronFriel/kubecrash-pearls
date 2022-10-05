using System.Collections.Generic;
using Pulumi;
using Civo = Pulumi.Civo;

return await Deployment.RunAsync(() => 
{
    var firewall = new Civo.Firewall("firewall", new()
    {
        Region = "NYC1",
        CreateDefaultRules = true,
    });

    var cluster = new Civo.KubernetesCluster("cluster", new()
    {
        Region = "NYC1",
        FirewallId = firewall.Id,
        Pools = new Civo.Inputs.KubernetesClusterPoolsArgs
        {
            NodeCount = 3,
            Size = "g4s.kube.medium",
        },
    });

    return new Dictionary<string, object?>
    {
        ["clusterName"] = cluster.Name,
    };
});

