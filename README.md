ssc
===

Simple State Capture of AWS accounts.

This project was created with the purpose of providing a historical capture of the current state of one or more accounts being used within Amazon Web Services (AWS).  The goal really is not to be a realtime monitoring engine, checkout the fantastic Edda project for that at: https://github.com/Netflix/edda

#### Main goals: 
* Precice and accurate capture of each describe/get/list api call pertinant to the AWS account.
* Minimize storage by only storing captures when something pertinant has changed within the state.
* Support a simple pluggable capture API for enabling, disabling, and extending what is captured.
* Support monitoring of multiple accounts.
* Provide framework for producing analytics on the captured data for metrics/reporting purposes.

#### Getting started:

Clone this repo.

```bash
git clone https://github.com/EquilibriumTechnologies/ssc.git
```

build.

``` bash
mvn clean package install
```

in ssc/ssc-dist/target there will be a bin tar.  Extract that to your desired running location.

For a single account monitoring, create an AwsCredentials.properties file in extracted_location/conf/ with appropriate accesses. (See below for a sample policy)

Modify ssc.properties accordingly, its got comments!

Run the main class com.eqt.ssc.SimpleStateCollector via the shell file found in extracted_location/bin.

#### Collection Capabilities
* autoscaling
* cloudformation
* cloudfront
* dynamodb
* **ec2** complete
* **elasticloadbalancing** complete
* elasticmapreduce
* glacier
* iam
* rds
* redshift
* route53
* **s3** complete
* sdb
* sns
* sqs

#### Account Management
##### Single Account
Use com.eqt.ssc.accounts.SameCredAccountManager
This will grab off your local ClasspathPropertiesFileCredentialsProvider that your account likely uses internally.
Really good for testing out things.

##### By ZK
Use com.eqt.ssc.accounts.ZookeeperMultiAccountManager
This stores data into ZK to allow for more than 1 account to be tracked.

#### REST API

SSC exposes a bare minimum REST api currently, found at localhost:8192 by default on each server running SSC.

List Known Accounts:

```bash
curl -X GET -H "Content-type:application/json" localhost:8182/api/managedAccounts
{"accounts":["222222222222","333333333333"]}
```

Add a new Account:

```bash
curl -X POST -H "Content-type:application/json" localhost:8182/api/addAccount -d \
'{"accountId":"222222222222","accessKey":"ANAWESOMEKEY","secretKey":"SOMEAWESOMEKEY","s3BucketName":"yourbucketloggings3","s3Path":"dir/with/s3/logs"}'
```

#### Credits
https://github.com/algesten/jsondiff for a sweet json diff engine, embedded locally into this souce.
http://tlrobinson.net/projects/javascript-fun/jsondiff/ for a very nice and simple json diff engine.

### Sample Audit Group policy
This is what I use, lets me audit what I want to within an account:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "autoscaling:DescribeAdjustmentTypes",
        "autoscaling:DescribeAutoScalingGroups",
        "autoscaling:DescribeAutoScalingInstances",
        "autoscaling:DescribeAutoScalingNotificationTypes",
        "autoscaling:DescribeLaunchConfigurations",
        "autoscaling:DescribeMetricCollectionTypes",
        "autoscaling:DescribeNotificationConfigurations",
        "autoscaling:DescribePolicies",
        "autoscaling:DescribeScalingActivities",
        "autoscaling:DescribeScalingProcessTypes",
        "autoscaling:DescribeScheduledActions",
        "autoscaling:DescribeTags",
        "autoscaling:DescribeTriggers",
        "cloudformation:DescribeStackEvents",
        "cloudformation:DescribeStackResource",
        "cloudformation:DescribeStackResources",
        "cloudformation:DescribeStacks",
        "cloudformation:GetTemplate",
        "cloudformation:ListStacks",
        "cloudformation:ListStackResources",
        "cloudfront:GetCloudFrontOriginAccessIdentity",
        "cloudfront:GetCloudFrontOriginAccessIdentityConfig",
        "cloudfront:GetDistribution",
        "cloudfront:GetDistributionConfig",
        "cloudfront:GetInvalidation",
        "cloudfront:GetStreamingDistribution",
        "cloudfront:GetStreamingDistributionConfig",
        "cloudfront:ListCloudFrontOriginAccessIdentities",
        "cloudfront:ListDistributions",
        "cloudfront:ListInvalidations",
        "cloudfront:ListStreamingDistributions",
        "cloudwatch:DescribeAlarms",
        "dynamodb:ListTables",
        "ec2:DescribeAddresses",
        "ec2:DescribeAvailabilityZones",
        "ec2:DescribeBundleTasks",
        "ec2:DescribeConversionTasks",
        "ec2:DescribeCustomerGateways",
        "ec2:DescribeDhcpOptions",
        "ec2:DescribeExportTasks",
        "ec2:DescribeImageAttribute",
        "ec2:DescribeImages",
        "ec2:DescribeInstanceAttribute",
        "ec2:DescribeInstanceStatus",
        "ec2:DescribeInstances",
        "ec2:DescribeInternetGateways",
        "ec2:DescribeKeyPairs",
        "ec2:DescribeLicenses",
        "ec2:DescribeNetworkAcls",
        "ec2:DescribeNetworkInterfaceAttribute",
        "ec2:DescribeNetworkInterfaces",
        "ec2:DescribePlacementGroups",
        "ec2:DescribeRegions",
        "ec2:DescribeReservedInstances",
        "ec2:DescribeReservedInstancesOfferings",
        "ec2:DescribeRouteTables",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeSnapshotAttribute",
        "ec2:DescribeSnapshots",
        "ec2:DescribeSpotDatafeedSubscription",
        "ec2:DescribeSpotInstanceRequests",
        "ec2:DescribeSpotPriceHistory",
        "ec2:DescribeSubnets",
        "ec2:DescribeTags",
        "ec2:DescribeVolumeAttribute",
        "ec2:DescribeVolumeStatus",
        "ec2:DescribeVolumes",
        "ec2:DescribeVpcPeeringConnection",
        "ec2:DescribeVpcs",
        "ec2:DescribeVpnConnections",
        "ec2:DescribeVpnGateways",
        "elasticloadbalancing:DescribeInstanceHealth",
        "elasticloadbalancing:DescribeLoadBalancers",
        "elasticmapreduce:DescribeJobFlows",
        "glacier:ListVaults",
        "iam:EnableMFADevice",
        "iam:GetAccountPasswordPolicy",
        "iam:GetAccountSummary",
        "iam:GetGroup",
        "iam:GetGroupPolicy",
        "iam:GetInstanceProfile",
        "iam:GetLoginProfile",
        "iam:GetRole",
        "iam:GetRolePolicy",
        "iam:GetServerCertificate",
        "iam:GetUser",
        "iam:GetUserPolicy",
        "iam:ListAccessKeys",
        "iam:ListAccountAliases",
        "iam:ListGroupPolicies",
        "iam:ListGroups",
        "iam:ListGroupsForUser",
        "iam:ListInstanceProfiles",
        "iam:ListInstanceProfilesForRole",
        "iam:ListMFADevices",
        "iam:ListRolePolicies",
        "iam:ListRoles",
        "iam:ListServerCertificates",
        "iam:ListSigningCertificates",
        "iam:ListUserPolicies",
        "iam:ListUsers",
        "iam:ListVirtualMFADevices",
        "rds:DescribeEngineDefaultParameters",
        "rds:DescribeDBInstances",
        "rds:DescribeDBLogFiles",
        "rds:DescribeDBParameterGroups",
        "rds:DescribeDBParameters",
        "rds:DescribeDBSecurityGroups",
        "rds:DescribeDBSnapshots",
        "rds:DescribeDBEngineVersions",
        "rds:DescribeDBSubnetGroups",
        "rds:DescribeEventCategories",
        "rds:DescribeEvents",
        "rds:DescribeEventSubscriptions",
        "rds:DescribeOptionGroups",
        "rds:DescribeOptionGroupOptions",
        "rds:DescribeOrderableDBInstanceOptions",
        "rds:DescribeReservedDBInstances",
        "rds:DescribeReservedDBInstancesOfferings",
        "rds:DownloadDBLogFilePortion",
        "rds:ListTagsForResource",
        "redshift:DescribeClusterParameterGroups",
        "redshift:DescribeClusterParameters",
        "redshift:DescribeClusterSecurityGroups",
        "redshift:DescribeClusterSnapshots",
        "redshift:DescribeClusterSubnetGroups",
        "redshift:DescribeClusterVersions",
        "redshift:DescribeClusters",
        "redshift:DescribeDefaultClusterParameters",
        "redshift:DescribeEvents",
        "redshift:DescribeOrderableClusterOptions",
        "redshift:DescribeReservedNodeOfferings",
        "redshift:DescribeReservedNodes",
        "redshift:DescribeResize",
        "route53:GetHostedZone",
        "route53:ListHostedZones",
        "route53:ListResourceRecordSets",
        "s3:GetBucketAcl",
        "s3:GetBucketLocation",
        "s3:GetBucketLogging",
        "s3:GetBucketNotification",
        "s3:GetBucketPolicy",
        "s3:GetBucketRequestPayment",
        "s3:GetBucketVersioning",
        "s3:GetBucketWebsite",
        "s3:GetLifecycleConfiguration",
        "s3:GetObjectAcl",
        "s3:GetObjectVersionAcl",
        "s3:ListAllMyBuckets",
        "sdb:DomainMetadata",
        "sdb:ListDomains",
        "sns:GetTopicAttributes",
        "sns:ListTopics",
        "sqs:GetQueueAttributes",
        "sqs:ListQueues"
      ],
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
```
