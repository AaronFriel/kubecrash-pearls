import * as pulumi from "@pulumi/pulumi";
import * as civo from "@pulumi/civo";

const firewall = new civo.Firewall("firewall", {
    region: "NYC1",
    createDefaultRules: true,
});
const cluster = new civo.KubernetesCluster("cluster", {
    region: "NYC1",
    firewallId: firewall.id,
    pools: {
        nodeCount: 3,
        size: "g4s.kube.medium",
    },
});
export const clusterName = cluster.name;
