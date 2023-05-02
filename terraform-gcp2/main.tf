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
  # 10h retention
  message_retention_duration = "36000s"

}

# Create bucket

resource "google_storage_bucket" "storage_bucket" {
  name               = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
  force_destroy      = var.force_destroy
  location           = var.location
  project            = var.storage_project
  storage_class      = var.storage_class
  labels             = var.labels

  versioning {
    enabled = var.versioning
  }
  logging {
    log_bucket        = var.log_bucket
    log_object_prefix = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
  }
}
# Create folder in a bucket
resource "google_storage_bucket_object" "content_folder" {
  name          = "export/"
  content       = "Not really a directory, but it's empty."
  bucket        = google_storage_bucket.storage_bucket.name
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