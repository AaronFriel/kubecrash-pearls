package main

import (
	helmv3 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/helm/v3"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		_, err := helmv3.NewRelease(ctx, "ingress-nginx", &helmv3.ReleaseArgs{
			Namespace: pulumi.String("admin-ingress-nginx"),
			Chart:     pulumi.String("ingress-nginx"),
			RepositoryOpts: &helmv3.RepositoryOptsArgs{
				Repo: pulumi.String("https://kubernetes.github.io/ingress-nginx"),
			},
			Values:          nil,
			CreateNamespace: pulumi.Bool(true),
		})
		if err != nil {
			return err
		}
		return nil
	})
}
