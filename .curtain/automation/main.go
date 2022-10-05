package main

import (
	"context"
	"fmt"
	"os"
	"strconv"

	"github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes"
	appsv1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/apps/v1"
	metav1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/meta/v1"
	"github.com/pulumi/pulumi/sdk/v3/go/auto"
	"github.com/pulumi/pulumi/sdk/v3/go/auto/optup"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func scale(replicas int) func(ctx *pulumi.Context) error {
	return func(ctx *pulumi.Context) error {
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
				Replicas: pulumi.Int(replicas),
			},
		}, pulumi.Provider(k8SProvider))
		if err != nil {
			return err
		}
		ctx.Export("status", deployment.Status)
		return nil
	}
}

func main() {
	// to destroy our program, we can run `go run main.go destroy`
	var err error
	replicas := 1
	argsWithoutProg := os.Args[1:]
	if len(argsWithoutProg) != 2 {
		fmt.Printf("Must provide two arguments: stack name and replicas")
	}
	stackName := argsWithoutProg[0]
	replicas, err = strconv.Atoi(argsWithoutProg[1])
	if err != nil {
		panic(err)
	}

	ctx := context.Background()

	s, err := auto.UpsertStackInlineSource(ctx, stackName, "6-patch", scale(replicas))

	fmt.Printf("Created/Selected stack %q\n", stackName)

	w := s.Workspace()

	// for inline source programs, we must manage plugins ourselves
	err = w.InstallPlugin(ctx, "kubernetes", "v3.21.4")
	if err != nil {
		fmt.Printf("Failed to install program plugins: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("Starting refresh")

	_, err = s.Refresh(ctx)
	if err != nil {
		fmt.Printf("Failed to refresh stack: %v\n", err)
		os.Exit(1)
	}

	fmt.Println("Refresh succeeded!")

	fmt.Println("Starting update")

	// wire up our update to stream progress to stdout
	stdoutStreamer := optup.ProgressStreams(os.Stdout)

	// run the update to deploy our s3 website
	_, err = s.Up(ctx, stdoutStreamer)
	if err != nil {
		fmt.Printf("Failed to update stack: %v\n\n", err)
		os.Exit(1)
	}
}
