using System.Collections.Generic;
using Pulumi;
using Kubernetes = Pulumi.Kubernetes;

return await Deployment.RunAsync(() =>
{
    var dbStack = new Pulumi.StackReference("dbStack", new()
    {
        Name = "friel/4-db/dev",
    });

    var wordpressSecret = new Kubernetes.Core.V1.Secret("wordpress-secret", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Namespace = "awesome-site",
        },
        StringData =
        {
            { "dbPassword", dbStack.GetOutput("DbPassword") },
        },
    });

    var wordpress = new Kubernetes.Apps.V1.Deployment("wordpress", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Name = "wordpress",
            Namespace = "awesome-site",
            Labels =
            {
                { "app", "wordpress" },
            },
        },
        Spec = new Kubernetes.Types.Inputs.Apps.V1.DeploymentSpecArgs
        {
            Selector = new Kubernetes.Types.Inputs.Meta.V1.LabelSelectorArgs
            {
                MatchLabels =
                {
                    { "app", "wordpress" },
                },
            },
            Replicas = 3,
            Template = new Kubernetes.Types.Inputs.Core.V1.PodTemplateSpecArgs
            {
                Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
                {
                    Labels =
                    {
                        { "app", "wordpress" },
                    },
                },
                Spec = new Kubernetes.Types.Inputs.Core.V1.PodSpecArgs
                {
                    Containers = new[]
                    {
                        new Kubernetes.Types.Inputs.Core.V1.ContainerArgs
                        {
                            Name = "wordpress",
                            Image = "wordpress:latest",
                            Ports = new[]
                            {
                                new Kubernetes.Types.Inputs.Core.V1.ContainerPortArgs
                                {
                                    ContainerPort = 80,
                                    Name = "wordpress",
                                },
                            },
                            Env = new[]
                            {
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "WORDPRESS_DB_NAME",
                                    Value = dbStack.GetOutput("DbName"),
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "WORDPRESS_DB_HOST",
                                    Value = dbStack.GetOutput("DbHost"),
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "WORDPRESS_DB_USER",
                                    Value = dbStack.GetOutput("DbUser"),
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "WORDPRESS_DB_PASSWORD",
                                    ValueFrom = new Kubernetes.Types.Inputs.Core.V1.EnvVarSourceArgs
                                    {
                                        SecretKeyRef = new Kubernetes.Types.Inputs.Core.V1.SecretKeySelectorArgs
                                        {
                                            Name = wordpressSecret.Metadata.Apply(metadata => metadata?.Name),
                                            Key = "dbPassword",
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    });

    var service = new Kubernetes.Core.V1.Service("service", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Namespace = "awesome-site",
            Name = "wordpress",
            Labels =
            {
                { "app", "wordpress" },
            },
        },
        Spec = new Kubernetes.Types.Inputs.Core.V1.ServiceSpecArgs
        {
            Selector =
            {
                { "app", "wordpress" },
            },
            Ports = new[]
            {
                new Kubernetes.Types.Inputs.Core.V1.ServicePortArgs
                {
                    Port = 80,
                    TargetPort = 80,
                    Protocol = "TCP",
                    Name = "wordpress",
                },
            },
            Type = "ClusterIP",
        },
    });

    var ingress = new Kubernetes.Networking.V1.Ingress("ingress", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Name = "ingress",
            Namespace = "awesome-site",
            Labels =
            {
                { "app", "wordpress" },
            },
        },
        Spec = new Kubernetes.Types.Inputs.Networking.V1.IngressSpecArgs
        {
            IngressClassName = "nginx",
            Rules = new[]
            {
                new Kubernetes.Types.Inputs.Networking.V1.IngressRuleArgs
                {
                    Http = new Kubernetes.Types.Inputs.Networking.V1.HTTPIngressRuleValueArgs
                    {
                        Paths = new[]
                        {
                            new Kubernetes.Types.Inputs.Networking.V1.HTTPIngressPathArgs
                            {
                                Path = "/",
                                PathType = "Prefix",
                                Backend = new Kubernetes.Types.Inputs.Networking.V1.IngressBackendArgs
                                {
                                    Service = new Kubernetes.Types.Inputs.Networking.V1.IngressServiceBackendArgs
                                    {
                                        Name = "wordpress",
                                        Port = new Kubernetes.Types.Inputs.Networking.V1.ServiceBackendPortArgs
                                        {
                                            Number = 80,
                                        },
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
    });

});
