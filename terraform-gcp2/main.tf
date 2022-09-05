# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.13.2"
}

provider "google" {
  version = ">= 4.26"
}
provider "kubernetes" {
  version = ">= 2.13.1"
}


#
resource "kubernetes_secret" "ror-kingu-db-password" {
  metadata {
  name      = "kingu-psql-credentials"
  namespace = var.kube_namespace
  }

  data = {
  "PGPASSWORD"  = var.ror-tiamat-db-password
  }
}