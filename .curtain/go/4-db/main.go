package main

import (
	appsv1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/apps/v1"
	corev1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/core/v1"
	metav1 "github.com/pulumi/pulumi-kubernetes/sdk/v3/go/kubernetes/meta/v1"
	"github.com/pulumi/pulumi-random/sdk/v4/go/random"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)

func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		namespace := "awesome-site"
		rootPassword, err := random.NewRandomPassword(ctx, "rootPassword", &random.RandomPasswordArgs{
			Length:  pulumi.Int(24),
			Special: pulumi.Bool(false),
		})
		if err != nil {
			return err
		}
		userPassword, err := random.NewRandomPassword(ctx, "userPassword", &random.RandomPasswordArgs{
			Length:  pulumi.Int(24),
			Special: pulumi.Bool(false),
		})
		if err != nil {
			return err
		}
		secretResource, err := corev1.NewSecret(ctx, "secret", &corev1.SecretArgs{
			Metadata: &metav1.ObjectMetaArgs{
				Namespace: pulumi.String(namespace),
			},
			StringData: pulumi.StringMap{
				"rootPassword": rootPassword.Result,
				"userPassword": userPassword.Result,
			},
		})
		if err != nil {
			return err
		}
		service, err := corev1.NewService(ctx, "service", &corev1.ServiceArgs{
			Metadata: &metav1.ObjectMetaArgs{
				Name:      pulumi.String("database-service"),
				Namespace: pulumi.String(namespace),
			},
			Spec: &corev1.ServiceSpecArgs{
				Ports: corev1.ServicePortArray{
					&corev1.ServicePortArgs{
						Port:       pulumi.Int(3306),
						TargetPort: pulumi.Any(3306),
					},
				},
				Selector: pulumi.StringMap{
					"app": pulumi.String("db"),
				},
			},
		})
		if err != nil {
			return err
		}
		_, err = appsv1.NewStatefulSet(ctx, "database", &appsv1.StatefulSetArgs{
			Metadata: &metav1.ObjectMetaArgs{
				Name:      pulumi.String("database"),
				Namespace: pulumi.String(namespace),
				Labels: pulumi.StringMap{
					"app": pulumi.String("db"),
				},
			},
			Spec: &appsv1.StatefulSetSpecArgs{
				Selector: &metav1.LabelSelectorArgs{
					MatchLabels: pulumi.StringMap{
						"app": pulumi.String("db"),
					},
				},
				ServiceName: pulumi.String("database-service"),
				Replicas:    pulumi.Int(1),
				Template: &corev1.PodTemplateSpecArgs{
					Metadata: &metav1.ObjectMetaArgs{
						Labels: pulumi.StringMap{
							"app": pulumi.String("db"),
						},
					},
					Spec: &corev1.PodSpecArgs{
						Containers: corev1.ContainerArray{
							&corev1.ContainerArgs{
								Name:  pulumi.String("database"),
								Image: pulumi.String("mariadb:10"),
								Args: pulumi.StringArray{
									pulumi.String("--default-authentication-plugin=mysql_native_password"),
								},
								Env: corev1.EnvVarArray{
									&corev1.EnvVarArgs{
										Name:  pulumi.String("MYSQL_DATABASE"),
										Value: pulumi.String("wordpress"),
									},
									&corev1.EnvVarArgs{
										Name:  pulumi.String("MYSQL_USER"),
										Value: pulumi.String("wordpress"),
									},
									&corev1.EnvVarArgs{
										Name: pulumi.String("MYSQL_PASSWORD"),
										ValueFrom: &corev1.EnvVarSourceArgs{
											SecretKeyRef: &corev1.SecretKeySelectorArgs{
												Name: secretResource.Metadata.ApplyT(func(metadata metav1.ObjectMeta) (string, error) {
													return metadata.Name, nil
												}).(pulumi.StringOutput),
												Key: pulumi.String("userPassword"),
											},
										},
									},
									&corev1.EnvVarArgs{
										Name: pulumi.String("MYSQL_ROOT_PASSWORD"),
										ValueFrom: &corev1.EnvVarSourceArgs{
											SecretKeyRef: &corev1.SecretKeySelectorArgs{
												Name: secretResource.Metadata.ApplyT(func(metadata metav1.ObjectMeta) (string, error) {
													return metadata.Name, nil
												}).(pulumi.StringOutput),
												Key: pulumi.String("rootPassword"),
											},
										},
									},
								},
								Ports: corev1.ContainerPortArray{
									&corev1.ContainerPortArgs{
										ContainerPort: pulumi.Int(3306),
										Name:          pulumi.String("mysql"),
									},
								},
								VolumeMounts: corev1.VolumeMountArray{
									&corev1.VolumeMountArgs{
										Name:      pulumi.String("database"),
										MountPath: pulumi.String("/var/lib/mysql"),
									},
								},
							},
						},
						Volumes: corev1.VolumeArray{
							&corev1.VolumeArgs{
								Name: pulumi.String("database"),
								PersistentVolumeClaim: &corev1.PersistentVolumeClaimVolumeSourceArgs{
									ClaimName: pulumi.String("database"),
								},
							},
						},
					},
				},
				VolumeClaimTemplates: []corev1.PersistentVolumeClaimTypeArgs{
					&corev1.PersistentVolumeClaimTypeArgs{
						Metadata: &metav1.ObjectMetaArgs{
							Name: pulumi.String("database"),
						},
						Spec: &corev1.PersistentVolumeClaimSpecArgs{
							AccessModes: pulumi.StringArray{
								pulumi.String("ReadWriteOnce"),
							},
							Resources: &corev1.ResourceRequirementsArgs{
								Requests: pulumi.StringMap{
									"storage": pulumi.String("1Gi"),
								},
							},
						},
					},
				},
			},
		})
		if err != nil {
			return err
		}
		ctx.Export("dbHost", service.Metadata.ApplyT(func(metadata metav1.ObjectMeta) (string, error) {
			return metadata.Name, nil
		}).(pulumi.StringOutput))
		ctx.Export("dbUser", "wordpress")
		ctx.Export("dbName", "wordpress")
		ctx.Export("dbPassword", userPassword.Result)
		return nil
	})
}
