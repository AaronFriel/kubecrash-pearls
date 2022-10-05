package main

import (
	"github.com/pulumi/pulumi-civo/sdk/v2/go/civo"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		firewall, err := civo.NewFirewall(ctx, "firewall", &civo.FirewallArgs{
			Region:             pulumi.String("NYC1"),
			CreateDefaultRules: pulumi.Bool(true),
		})
		if err != nil {
			return err
		}
		cluster, err := civo.NewKubernetesCluster(ctx, "cluster", &civo.KubernetesClusterArgs{
			Region:     pulumi.String("NYC1"),
			FirewallId: firewall.ID(),
			Pools: &KubernetesClusterPoolsArgs{
				NodeCount: pulumi.Int(3),
				Size:      pulumi.String("g4s.kube.medium"),
			},
		})
		if err != nil {
			return err
		}
		ctx.Export("clusterName", cluster.Name)
		return nil
	})
}
