package main

import (
	"github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes"
	corev1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/core/v1"
	metav1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/meta/v1"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi/config"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		cfg := config.New(ctx, "")
		kubernetesKubeconfig := cfg.Require("kubernetesKubeconfig")
		k8SProvider, err := kubernetes.NewProvider(ctx, "k8sProvider", &kubernetes.ProviderArgs{
			Kubeconfig: pulumi.String(kubernetesKubeconfig),
		})
		if err != nil {
			return err
		}
		awesomeSite, err := corev1.NewNamespace(ctx, "awesome-site", &corev1.NamespaceArgs{
			Metadata: &metav1.ObjectMetaArgs{
				Name: pulumi.String("awesome-site"),
			},
		}, pulumi.Provider(k8SProvider))
		if err != nil {
			return err
		}
		ctx.Export("name", awesomeSite.Metadata.Elem().Name())
		return nil
	})
}
