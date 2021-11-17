# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.12"
}

provider "google" {
  version = "~> 2.19"
}
provider "kubernetes" {
  version = "~> 1.13.3"
  load_config_file = var.load_config_file
}

# Create bucket

resource "google_storage_bucket" "storage_bucket" {
  name               = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
  force_destroy      = var.force_destroy
  location           = var.location
  project            = var.storage_project
  storage_class      = var.storage_class
  bucket_policy_only = var.bucket_policy_only
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


# create service account
resource "google_service_account" "kingu_service_account" {
  account_id   = "${var.labels.team}-${var.labels.app}-sa"
  display_name = "${var.labels.team}-${var.labels.app} service account"
  project = var.gcp_project
}

# add service account as member to the bucket
resource "google_storage_bucket_iam_member" "storage_bucket_iam_member" {
  bucket = google_storage_bucket.storage_bucket.name
  role   = var.service_account_bucket_role
  member = "serviceAccount:${google_service_account.kingu_service_account.email}"
}

# add service account as member to the cloudsql client
resource "google_project_iam_member" "project" {
  project = var.cloudsql_project
  role    = var.service_account_cloudsql_role
  member = "serviceAccount:${google_service_account.kingu_service_account.email}"
}


# create key for service account
resource "google_service_account_key" "kingu_service_account_key" {
  service_account_id = google_service_account.kingu_service_account.name
}

  # Add SA key to to k8s
resource "kubernetes_secret" "tiamat_service_account_credentials" {
  metadata {
    name      = "${var.labels.team}-${var.labels.app}-sa-key"
    namespace = var.kube_namespace
  }
  data = {
    "credentials.json" = "${base64decode(google_service_account_key.kingu_service_account_key.private_key)}"
  }
}
#
resource "kubernetes_secret" "ror-kingu-db-password" {
  metadata {
  name      = "${var.labels.team}-${var.labels.app}-db-password"
  namespace = var.kube_namespace
  }

  data = {
  "password"     = var.ror-kingu-db-password
  }

  # pubsub topic and sub


}

# Create pubsub topics and subscriptions

resource "google_pubsub_topic" "KinguExportIncomingQueue" {
  name = "KinguExportIncomingQueue"
  project = var.gcp_pubsub_project
  labels = var.labels
}

resource "google_pubsub_subscription" "KinguExportIncomingQueue" {
  name = "KinguExportIncomingQueue"
  topic = google_pubsub_topic.KinguExportIncomingQueue.name
  project = var.gcp_pubsub_project
  labels = var.labels
  retry_policy {
    minimum_backoff = "10s"
  }
}

resource "google_pubsub_topic" "KinguExportOutgoingQueue" {
  name = "KinguExportOutgoingQueue"
  project = var.gcp_pubsub_project
  labels = var.labels
}

resource "google_pubsub_subscription" "KinguExportOutgoingQueue" {
  name = "KinguExportOutgoingQueue"
  topic = google_pubsub_topic.KinguExportOutgoingQueue.name
  project = var.gcp_pubsub_project
  labels = var.labels
  retry_policy {
    minimum_backoff = "10s"
  }
}
# add service account as member to pubsub service in the resources project
resource "google_project_iam_member" "pubsub_project_iam_member_subscriber" {
  project = var.gcp_pubsub_project
  role = "roles/pubsub.subscriber"
  member = "serviceAccount:${google_service_account.kingu_service_account.email}"
}

resource "google_project_iam_member" "pubsub_project_iam_member_publisher" {
  project = var.gcp_pubsub_project
  role = "roles/pubsub.publisher"
  member = "serviceAccount:${google_service_account.kingu_service_account.email}"
}