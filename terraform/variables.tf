variable "storage_project" {
  description = "GCP project of storage"
}
variable "pubsub_project" {
  description = "pubsub project name"
}

variable "kube_namespace" {
  description = "The Kubernetes namespace"
  default = "kingu"
}

variable "labels" {
  description = "Labels used in all resources"
  type        = map(string)
     default = {
       manager = "terraform"
       team    = "ror"
       slack   = "talk-ror"
       app     = "kingu"
     }
}

variable "location" {
  description = "GCP bucket location"
  default = "europe-west1"
}

variable "bucket_instance_suffix" {
  description = "A suffix for the bucket instance, may be changed if environment is destroyed and then needed again (name collision workaround) - also bucket names must be globally unique"
}

variable "bucket_instance_prefix" {
  description = "A prefix for the bucket instance, may be changed if environment is destroyed and then needed again (name collision workaround) - also bucket names must be globally unique"
  default = "ror-kingu"
}

variable "force_destroy" {
  description = "(Optional, Default: false) When deleting a bucket, this boolean option will delete all contained objects. If you try to delete a bucket that contains objects, Terraform will fail that run"
  default     = false
}

variable "storage_class" {
  description = "GCP storage class"
  default     = "STANDARD"
}

variable "versioning" {
  description = "The bucket's Versioning configuration."
  default     = "false"
}

variable "log_bucket" {
  description = "The bucket's Access & Storage Logs configuration"
  default     = "false"
}


variable ror-kingu-db-password {
  description = "Tiamat database password"
}

variable "kingu_netex_export_topic" {
  default = "ror.kingu.outbound.topic.netex.export"
}

variable "kingu_netex_export_subscription" {
  default = "ror.kingu.inbound.subscription.kakka.netex.export"
}