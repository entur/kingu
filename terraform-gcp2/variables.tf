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

variable ror-tiamat-db-password {
  description = "Tiamat database password"
}