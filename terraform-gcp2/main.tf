# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.13.2"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 4.26"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.13.1"
    }
  }
}

resource "google_pubsub_topic" "kingu_netex_export_topic" {
  name = var.kingu_netex_export_topic
  project = var.pubsub_project
  labels = var.labels
}

resource "google_pubsub_subscription" "kingu_netex_export_subscription" {
  name =var.kingu_netex_export_subscription
  topic =google_pubsub_topic.kingu_netex_export_topic.name
  filter = "attributes.EnturNetexExportStatus = \"Initiated\""
  project = var.pubsub_project
  labels = var.labels
  ack_deadline_seconds = 10
  retry_policy {
    minimum_backoff = "10s"
    maximum_backoff = "600s"
  }
  message_retention_duration = "10h"

}


#
resource "kubernetes_secret" "ror-kingu-db-password" {
  metadata {
  name      = "kingu-psql-credentials"
  namespace = var.kube_namespace
  }

  data = {
  "PGPASSWORD"  = var.ror-kingu-db-password
  }
}