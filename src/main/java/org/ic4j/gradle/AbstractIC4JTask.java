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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaOkHttpTransport;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.identity.Secp256k1Identity;
import org.ic4j.management.ManagementService;
import org.ic4j.types.Principal;

abstract class AbstractIC4JTask extends DefaultTask {
	static final Principal effectiveCanister = Principal.fromString("x5pps-pqaaa-aaaab-qadbq-cai");
	
	static final String DEFAULT_NETWORK = "http://localhost:4943/";
	
	@Input
	String identity;
	
	@Input
	String network = DEFAULT_NETWORK;
	
	
	@Input
	NamedDomainObjectContainer<Canister> canisters = this.getProject().container(Canister.class);


	/**
	 * @return the canisters
	 */

    void canisters(final Action<? super NamedDomainObjectContainer<Canister>> action) {
        action.execute(canisters);
    }
    
    @Internal
	protected ManagementService getManagementService() throws FileNotFoundException, URISyntaxException {
		
		Reader sourceReader = new FileReader(this.identity);
		
		Identity identity = Secp256k1Identity.fromPEMFile(sourceReader);


		ReplicaTransport transport = ReplicaOkHttpTransport.create(this.network);
		Agent agent = new AgentBuilder().transport(transport)
				.identity(identity)
				.build();
		agent.fetchRootKey();

		ManagementService managementService = ManagementService.create(agent, Principal.managementCanister(),effectiveCanister);
		
		return managementService;
	

}   
}
