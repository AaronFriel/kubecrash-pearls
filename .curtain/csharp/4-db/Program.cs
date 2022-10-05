using System.Collections.Generic;
using Pulumi;
using Kubernetes = Pulumi.Kubernetes;
using Random = Pulumi.Random;

return await Deployment.RunAsync(() => 
{
    var @namespace = "awesome-site";

    var rootPassword = new Random.RandomPassword("rootPassword", new()
    {
        Length = 24,
        Special = false,
    });

    var userPassword = new Random.RandomPassword("userPassword", new()
    {
        Length = 24,
        Special = false,
    });

    var secretResource = new Kubernetes.Core.V1.Secret("secret", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Namespace = @namespace,
        },
        StringData = 
        {
            { "rootPassword", rootPassword.Result },
            { "userPassword", userPassword.Result },
        },
    });

    var service = new Kubernetes.Core.V1.Service("service", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Name = "database-service",
            Namespace = @namespace,
        },
        Spec = new Kubernetes.Types.Inputs.Core.V1.ServiceSpecArgs
        {
            Ports = new[]
            {
                new Kubernetes.Types.Inputs.Core.V1.ServicePortArgs
                {
                    Port = 3306,
                    TargetPort = 3306,
                },
            },
            Selector = 
            {
                { "app", "db" },
            },
        },
    });

    var database = new Kubernetes.Apps.V1.StatefulSet("database", new()
    {
        Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
        {
            Name = "database",
            Namespace = @namespace,
            Labels = 
            {
                { "app", "db" },
            },
        },
        Spec = new Kubernetes.Types.Inputs.Apps.V1.StatefulSetSpecArgs
        {
            Selector = new Kubernetes.Types.Inputs.Meta.V1.LabelSelectorArgs
            {
                MatchLabels = 
                {
                    { "app", "db" },
                },
            },
            ServiceName = "database-service",
            Replicas = 1,
            Template = new Kubernetes.Types.Inputs.Core.V1.PodTemplateSpecArgs
            {
                Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
                {
                    Labels = 
                    {
                        { "app", "db" },
                    },
                },
                Spec = new Kubernetes.Types.Inputs.Core.V1.PodSpecArgs
                {
                    Containers = new[]
                    {
                        new Kubernetes.Types.Inputs.Core.V1.ContainerArgs
                        {
                            Name = "database",
                            Image = "mariadb:10",
                            Args = new[]
                            {
                                "--default-authentication-plugin=mysql_native_password",
                            },
                            Env = new[]
                            {
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "MYSQL_DATABASE",
                                    Value = "wordpress",
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "MYSQL_USER",
                                    Value = "wordpress",
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "MYSQL_PASSWORD",
                                    ValueFrom = new Kubernetes.Types.Inputs.Core.V1.EnvVarSourceArgs
                                    {
                                        SecretKeyRef = new Kubernetes.Types.Inputs.Core.V1.SecretKeySelectorArgs
                                        {
                                            Name = secretResource.Metadata.Apply(metadata => metadata?.Name),
                                            Key = "userPassword",
                                        },
                                    },
                                },
                                new Kubernetes.Types.Inputs.Core.V1.EnvVarArgs
                                {
                                    Name = "MYSQL_ROOT_PASSWORD",
                                    ValueFrom = new Kubernetes.Types.Inputs.Core.V1.EnvVarSourceArgs
                                    {
                                        SecretKeyRef = new Kubernetes.Types.Inputs.Core.V1.SecretKeySelectorArgs
                                        {
                                            Name = secretResource.Metadata.Apply(metadata => metadata?.Name),
                                            Key = "rootPassword",
                                        },
                                    },
                                },
                            },
                            Ports = new[]
                            {
                                new Kubernetes.Types.Inputs.Core.V1.ContainerPortArgs
                                {
                                    ContainerPort = 3306,
                                    Name = "mysql",
                                },
                            },
                            VolumeMounts = new[]
                            {
                                new Kubernetes.Types.Inputs.Core.V1.VolumeMountArgs
                                {
                                    Name = "database",
                                    MountPath = "/var/lib/mysql",
                                },
                            },
                        },
                    },
                    Volumes = new[]
                    {
                        new Kubernetes.Types.Inputs.Core.V1.VolumeArgs
                        {
                            Name = "database",
                            PersistentVolumeClaim = new Kubernetes.Types.Inputs.Core.V1.PersistentVolumeClaimVolumeSourceArgs
                            {
                                ClaimName = "database",
                            },
                        },
                    },
                },
            },
            VolumeClaimTemplates = new[]
            {
                new Kubernetes.Types.Inputs.Core.V1.PersistentVolumeClaimArgs
                {
                    Metadata = new Kubernetes.Types.Inputs.Meta.V1.ObjectMetaArgs
                    {
                        Name = "database",
                    },
                    Spec = new Kubernetes.Types.Inputs.Core.V1.PersistentVolumeClaimSpecArgs
                    {
                        AccessModes = new[]
                        {
                            "ReadWriteOnce",
                        },
                        Resources = new Kubernetes.Types.Inputs.Core.V1.ResourceRequirementsArgs
                        {
                            Requests = 
                            {
                                { "storage", "1Gi" },
                            },
                        },
                    },
                },
            },
        },
    });

    return new Dictionary<string, object?>
    {
        ["dbHost"] = service.Metadata.Apply(metadata => metadata?.Name),
        ["dbUser"] = "wordpress",
        ["dbName"] = "wordpress",
        ["dbPassword"] = userPassword.Result,
    };
});

