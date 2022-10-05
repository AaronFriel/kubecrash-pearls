import pulumi
import pulumi_civo as civo

firewall = civo.Firewall("firewall",
    region="NYC1",
    create_default_rules=True)
cluster = civo.KubernetesCluster("cluster",
    region="NYC1",
    firewall_id=firewall.id,
    pools=civo.KubernetesClusterPoolsArgs(
        node_count=3,
        size="g4s.kube.medium",
    ))
pulumi.export("clusterName", cluster.name)
