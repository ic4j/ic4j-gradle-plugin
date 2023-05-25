/*
 * Copyright 2023 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ic4j.gradle;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.ic4j.agent.AgentError;
import org.ic4j.management.CanisterStatusResponse;
import org.ic4j.management.ManagementService;
import org.ic4j.types.Principal;

public class Uninstall extends AbstractIC4JTask {
	

	@TaskAction
	void run() {

		Security.addProvider(new BouncyCastleProvider());

		if (this.canisters == null || this.canisters.isEmpty())
			throw new GradleException("Undefined canister deployments");

		this.getLogger().info("ICP network " + this.network);

		if (this.canisters == null || this.canisters.isEmpty())
			throw new GradleException("Undefined canister deployments");

		try {
			ManagementService managementService = this.getManagementService();
			for (Canister deploymentCanister : this.canisters) {
				String canisterId = deploymentCanister.canisterId;

				if (canisterId == null)
					continue;

				Principal canister = Principal.fromString(canisterId);

				managementService.stopCanister(canister);

				CanisterStatusResponse canisterStatusResponse = managementService.canisterStatus(canister).get();

				this.getLogger()
						.info("Canister " + canister.toString() + " is " + canisterStatusResponse.status.name());

				managementService.uninstallCode(canister);

				if (deploymentCanister.delete) {
					managementService.deleteCanister(canister);
					this.getLogger().info("Canister " + canister.toString() + " deleted");
				}
			}
		} catch (AgentError e) {
			this.getLogger().error(e.getLocalizedMessage());
			throw new GradleException(e.getLocalizedMessage());
		} catch (Exception e) {
			this.getLogger().error(e.getLocalizedMessage());
			throw new GradleException(e.getLocalizedMessage());
		}
		;
	}

}
