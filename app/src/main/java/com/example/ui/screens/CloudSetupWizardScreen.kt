package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WizardStep
import com.example.ui.components.EuiCard
import com.example.ui.components.EuiCopyField
import com.example.ui.theme.LocalEuiStatusColors
import com.example.ui.viewmodel.PocketCoreViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CloudSetupWizardScreen(
  viewModel: PocketCoreViewModel,
  modifier: Modifier = Modifier
) {
  val wizardState by viewModel.wizardState.collectAsState()
  val statusColors = LocalEuiStatusColors.current

  BackHandler {
    if (wizardState.currentStep == WizardStep.SUCCESS || wizardState.currentStep == WizardStep.WELCOME) {
      viewModel.navigateBack()
    } else if (!wizardState.isDeploying) {
      val prevOrdinal = (wizardState.currentStep.ordinal - 1).coerceAtLeast(0)
      viewModel.setWizardStep(WizardStep.values()[prevOrdinal])
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("cloud_setup_wizard_screen")
  ) {
    // Header
    Surface(
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          Column {
            Text(
              text = "Cloud Setup Wizard",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Step ${wizardState.currentStep.stepNumber} of 7: ${wizardState.currentStep.title}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Progress bar across the top
    LinearProgressIndicator(
      progress = { wizardState.currentStep.stepNumber / 7f },
      color = MaterialTheme.colorScheme.secondary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
    )

    // Step Body
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (wizardState.currentStep) {
        WizardStep.WELCOME -> StepWelcome(onNext = { viewModel.setWizardStep(WizardStep.GOOGLE_AUTH) })
        WizardStep.GOOGLE_AUTH -> StepGoogleAuth(
          account = wizardState.selectedGoogleAccount,
          onNext = { viewModel.setWizardStep(WizardStep.GCP_PROJECT) }
        )
        WizardStep.GCP_PROJECT -> StepGcpProject(
          selectedProject = wizardState.selectedProjectId,
          onNext = { viewModel.setWizardStep(WizardStep.INFRA_CONFIG) }
        )
        WizardStep.INFRA_CONFIG -> StepInfraConfig(
          serverName = wizardState.serverName,
          region = wizardState.region,
          machineType = wizardState.machineType,
          diskSize = wizardState.diskSizeGb,
          version = wizardState.bedrockVersion,
          onConfigChange = { name, reg, mach, disk, ver ->
            viewModel.updateWizardConfig(name, reg, mach, disk, ver)
          },
          onNext = { viewModel.setWizardStep(WizardStep.COST_ESTIMATE) }
        )
        WizardStep.COST_ESTIMATE -> StepCostEstimate(
          machineType = wizardState.machineType,
          diskSize = wizardState.diskSizeGb,
          termsAccepted = wizardState.termsAccepted,
          onTermsChange = { viewModel.setWizardTermsAccepted(it) },
          onDeploy = { viewModel.triggerDeployment() }
        )
        WizardStep.DEPLOYING -> StepDeploying(
          progress = wizardState.deploymentProgress,
          steps = wizardState.deploymentSteps
        )
        WizardStep.SUCCESS -> StepSuccess(
          ip = wizardState.deployedIp,
          port = wizardState.deployedPort,
          onFinish = {
            viewModel.navigateTo(Screen.DASHBOARD)
          }
        )
      }
    }
  }
}

@Composable
private fun StepWelcome(onNext: () -> Unit) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Icon(
          imageVector = Icons.Outlined.CloudQueue,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Deploy Dedicated Bedrock Infrastructure",
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "PocketCore Cloud provisions a high-performance PocketMine-MP dedicated server instance inside your Google Cloud Platform organization with automated backups, DDoS mitigation, and low-latency RakNet routing.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    item {
      EuiCard {
        Text("WHAT THIS WIZARD CONFIGURES:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        FeatureBullet("Google Cloud Compute Engine VM instance")
        FeatureBullet("Firewall rules for Minecraft Bedrock UDP port 19132")
        FeatureBullet("PHP 8.2 runtime & PocketMine-MP 5 container")
        FeatureBullet("Automated persistent snapshot backup schedules")
        FeatureBullet("Zero-trust cloud monitoring & security logs")
      }
    }

    item {
      Button(
        onClick = onNext,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_step1_next_button")
      ) {
        Text("Get Started", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
      }
    }
  }
}

@Composable
private fun FeatureBullet(text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Check,
      contentDescription = null,
      tint = LocalEuiStatusColors.current.success,
      modifier = Modifier.size(16.dp)
    )
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun StepGoogleAuth(account: String, onNext: () -> Unit) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Text(
          text = "Google Account Authorization",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "PocketCore Cloud interacts with Google Cloud Platform APIs on your behalf. We will verify billing and API permissions for Compute Engine.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Account card
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Connected Account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(account, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(imageVector = Icons.Default.Check, contentDescription = "Authorized", tint = LocalEuiStatusColors.current.success)
          }
        }
      }
    }

    item {
      EuiCard {
        Text("REQUESTED PERMISSIONS & REASONS:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        PermissionItem("compute.instances.create", "Provision VM to host your server process")
        PermissionItem("compute.firewalls.create", "Allow external Bedrock players to join on UDP port")
        PermissionItem("storage.buckets.create", "Store encrypted automated server world snapshots")
      }
    }

    item {
      Button(
        onClick = onNext,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_step2_next_button")
      ) {
        Text("Authorize & Continue", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun PermissionItem(scope: String, reason: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = scope, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    Text(text = reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun StepGcpProject(selectedProject: String, onNext: () -> Unit) {
  var currentProject by remember { mutableStateOf(selectedProject) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Text("Google Cloud Project Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Select the target GCP Project ID where infrastructure will be provisioned.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(14.dp))

        val projects = listOf("pocketcore-prod-39440", "minecraft-cloud-infra", "bedrock-cluster-west")
        projects.forEach { proj ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (currentProject == proj) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, if (currentProject == proj) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline),
            onClick = { currentProject = proj },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(proj, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Billing Active • Compute Engine API Enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              if (currentProject == proj) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              }
            }
          }
        }
      }
    }

    item {
      Button(
        onClick = onNext,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_step3_next_button")
      ) {
        Text("Confirm Project", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun StepInfraConfig(
  serverName: String,
  region: String,
  machineType: String,
  diskSize: Int,
  version: String,
  onConfigChange: (String, String, String, Int, String) -> Unit,
  onNext: () -> Unit
) {
  var name by remember { mutableStateOf(serverName) }
  var selectedRegion by remember { mutableStateOf(region) }
  var selectedMachine by remember { mutableStateOf(machineType) }
  var disk by remember { mutableStateOf(diskSize.toString()) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Text("Infrastructure Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Server Instance Name") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("GCP REGION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        val regions = listOf("us-central1 (Iowa)", "us-east1 (S. Carolina)", "europe-west1 (Belgium)", "asia-east1 (Taiwan)")
        regions.forEach { reg ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedRegion == reg) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
              .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = selectedRegion == reg, onClick = { selectedRegion = reg })
            Text(reg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("MACHINE SPECIFICATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        val machines = listOf(
          "e2-standard-2 (2 vCPU, 8 GB RAM)",
          "e2-standard-4 (4 vCPU, 16 GB RAM)",
          "e2-medium (1 vCPU, 4 GB RAM)"
        )
        machines.forEach { mach ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedMachine == mach) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
              .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = selectedMachine == mach, onClick = { selectedMachine = mach })
            Text(mach, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }

    item {
      Button(
        onClick = {
          onConfigChange(name, selectedRegion, selectedMachine, disk.toIntOrNull() ?: 64, version)
          onNext()
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_step4_next_button")
      ) {
        Text("Continue to Cost Estimate", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun StepCostEstimate(
  machineType: String,
  diskSize: Int,
  termsAccepted: Boolean,
  onTermsChange: (Boolean) -> Unit,
  onDeploy: () -> Unit
) {
  val isStandard4 = machineType.contains("standard-4")
  val computeCost = if (isStandard4) "$49.60/month" else "$24.80/month"
  val diskCost = "$4.20/month"
  val totalCost = if (isStandard4) "$53.80/month" else "$29.00/month"

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Text("Transparent Cost Estimate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Google Cloud infrastructure charges are billed directly through your Google Cloud account. No surprise markup.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(14.dp))

        CostRow("Compute Engine ($machineType)", computeCost)
        CostRow("Balanced Persistent Disk (${diskSize} GB)", diskCost)
        CostRow("DDoS-protected Egress Network", "Included ($0.00)")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("ESTIMATED MONTHLY TOTAL", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Text(totalCost, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
      }
    }

    item {
      EuiCard {
        Text("Explicit User Approval", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = termsAccepted,
            onCheckedChange = onTermsChange,
            modifier = Modifier.testTag("wizard_terms_checkbox")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "I explicitly approve provisioning the specified Google Cloud infrastructure and accept standard Compute Engine resource charges.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    item {
      Button(
        onClick = onDeploy,
        enabled = termsAccepted,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_deploy_button")
      ) {
        Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Deploy Server Infrastructure", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun CostRow(label: String, cost: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(cost, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun StepDeploying(
  progress: Float,
  steps: List<com.example.data.model.DeploymentStep>
) {
  val statusColors = LocalEuiStatusColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Text("Deploying Cloud Infrastructure", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Provisioning and initializing your PocketMine-MP dedicated server container. Please do not close this screen.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
          progress = { progress },
          color = MaterialTheme.colorScheme.secondary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
      }
    }

    items(steps) { step ->
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (step.isCompleted) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = statusColors.success, modifier = Modifier.size(20.dp))
          } else if (step.isInProgress) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.secondary)
          } else {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
            )
          }

          Column {
            Text(step.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(step.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
private fun StepSuccess(ip: String, port: Int, onFinish: () -> Unit) {
  val statusColors = LocalEuiStatusColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      EuiCard {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusColors.successContainer,
          modifier = Modifier.size(44.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = statusColors.onSuccessContainer, modifier = Modifier.size(24.dp))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Deployment Successful!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Your Bedrock server is now operational and accepting player connections on UDP port 19132.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))
        EuiCopyField(label = "BEDROCK CONNECTION ADDRESS", value = "$ip:$port")
      }
    }

    item {
      Button(
        onClick = onFinish,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("wizard_finish_button")
      ) {
        Text("Open Dashboard", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}
