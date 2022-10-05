package main

import (
	"github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes"
	appsv1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/apps/v1"
	metav1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/meta/v1"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		k8SProvider, err := kubernetes.NewProvider(ctx, "k8sProvider", &kubernetes.ProviderArgs{
			EnableServerSideApply: pulumi.Bool(true),
		})
		if err != nil {
			return err
		}
		deployment, err := appsv1.NewDeploymentPatch(ctx, "deployment", &appsv1.DeploymentPatchArgs{
			Metadata: &metav1.ObjectMetaPatchArgs{
				Namespace: pulumi.String("awesome-site"),
				Name:      pulumi.String("wordpress"),
				Annotations: pulumi.StringMap{
					"pulumi.com/patchForce": pulumi.String("true"),
				},
			},
			Spec: &appsv1.DeploymentSpecPatchArgs{
				Replicas: pulumi.Int(3),
			},
		}, pulumi.Provider(k8SProvider))
		if err != nil {
			return err
		}
		ctx.Export("status", deployment.Status)
		return nil
	})
}
