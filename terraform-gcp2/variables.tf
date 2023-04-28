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

variable ror-kingu-db-password {
  description = "Tiamat database password"
}

variable "kingu_netex_export_topic" {
  default = "ror.kingu.outbound.topic.netex.export"
}
variable "pubsub_project" {
  description = "pubsub project name"
}
variable "kingu_netex_export_subscription" {
  default = "ror.kingu.inbound.subscription.kakka.netex.export"
}