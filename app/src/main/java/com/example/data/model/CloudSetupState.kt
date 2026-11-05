package com.example.data.model

enum class WizardStep(val stepNumber: Int, val title: String) {
  WELCOME(1, "Welcome"),
  GOOGLE_AUTH(2, "Google Authorization"),
  GCP_PROJECT(3, "GCP Project"),
  INFRA_CONFIG(4, "Infrastructure"),
  COST_ESTIMATE(5, "Cost & Confirmation"),
  DEPLOYING(6, "Deployment"),
  SUCCESS(7, "Deployment Success")
}

data class DeploymentStep(
  val title: String,
  val detail: String,
  val isCompleted: Boolean = false,
  val isInProgress: Boolean = false
)

data class CloudSetupState(
  val currentStep: WizardStep = WizardStep.WELCOME,
  val selectedGoogleAccount: String = "yesboy200005@gmail.com",
  val isAccountAuthorized: Boolean = true,
  val selectedProjectId: String = "pocketcore-prod-39440",
  val serverName: String = "Bedrock-Production-02",
  val region: String = "us-central1 (Iowa)",
  val machineType: String = "e2-standard-2 (2 vCPU, 8 GB RAM)",
  val diskSizeGb: Int = 64,
  val bedrockVersion: String = "v1.21.50 (PocketMine-MP 5.14)",
  val termsAccepted: Boolean = false,
  val isDeploying: Boolean = false,
  val deploymentProgress: Float = 0.0f,
  val deploymentSteps: List<DeploymentStep> = emptyList(),
  val deployedIp: String = "34.122.90.114",
  val deployedPort: Int = 19132
)
