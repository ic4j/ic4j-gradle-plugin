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

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.AgentError;
import org.ic4j.agent.QueryBuilder;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.UpdateBuilder;
import org.ic4j.agent.http.ReplicaOkHttpTransport;
import org.ic4j.agent.identity.AnonymousIdentity;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.identity.Secp256k1Identity;
import org.ic4j.candid.jackson.JacksonDeserializer;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.types.Mode;
import org.ic4j.types.Principal;

import com.fasterxml.jackson.databind.JsonNode;

public class Call extends AbstractIC4JTask {

	@TaskAction
	void run() {

		Security.addProvider(new BouncyCastleProvider());

		this.getLogger().info("ICP network " + this.network);

		if (this.canisters == null || this.canisters.isEmpty())
			throw new GradleException("Undefined canister deployments");

		try {

			for (Canister deployment : this.canisters) {

				if (deployment.canisterId == null || deployment.method == null)
					continue;

				Principal canister = Principal.fromString(deployment.canisterId);

				Identity identity = new AnonymousIdentity();
						
				if(this.identity != null)
				{	
					Reader sourceReader = new FileReader(this.identity);
					
					if(identityType != null && "basic".equals(identityType))
						identity = BasicIdentity.fromPEMFile(sourceReader);
					else	
						identity = Secp256k1Identity.fromPEMFile(sourceReader);
				}

				ReplicaTransport transport = ReplicaOkHttpTransport.create(this.network);
				Agent agent = new AgentBuilder().transport(transport).identity(identity).build();

				if (this.isLocal)
					agent.fetchRootKey();

				if (deployment.candid != null) {
					Path path = Paths.get(deployment.candid);
				}

				byte[] args = ArrayUtils.EMPTY_BYTE_ARRAY;

				if (deployment.argument != null) {
					this.getLogger().info("Argument " + deployment.argument);
					switch (deployment.argumentType) {
					case raw:
						args = deployment.argument.getBytes();
						break;
					default:
						IDLArgs idlArgs = IDLArgs.fromIDL(deployment.argument);
						args = idlArgs.toBytes();
						break;
					}
				}
				else
				{
					IDLArgs idlArgs = IDLArgs.create(new ArrayList<IDLValue>());
					args = idlArgs.toBytes();
				}

				if (deployment.methodType == null) {
					UpdateBuilder updateBuilder = UpdateBuilder.create(agent, canister, deployment.method).arg(args);

					CompletableFuture<byte[]> response = updateBuilder
							.callAndWait(org.ic4j.agent.Waiter.create(deployment.timeout, deployment.sleep));

					byte[] output = response.get();
					IDLArgs outArgs = IDLArgs.fromBytes(output);

					int i = 0;
					
					for(IDLValue value : outArgs.getArgs())
					{	
						JsonNode jsonResult = value.getValue(JacksonDeserializer.create(),JsonNode.class);
	
						this.getLogger().info("arg" + i + "=" +jsonResult.asText());
						i++;
					}

				}else if(deployment.methodType == Mode.QUERY)
				{
					
					CompletableFuture<byte[]> response = QueryBuilder.create(agent, canister, deployment.method).arg(args).call();
					
					byte[] output = response.get();
					IDLArgs outArgs = IDLArgs.fromBytes(output);
					
					int i = 0;
					
					for(IDLValue value : outArgs.getArgs())
					{	
						JsonNode jsonResult = value.getValue(JacksonDeserializer.create(),JsonNode.class);
	
						this.getLogger().info("arg" + i + "=" +jsonResult.asText());
						i++;
					}
					
				}else if(deployment.methodType == Mode.ONEWAY)
				{
					UpdateBuilder updateBuilder = UpdateBuilder.create(agent, canister, deployment.method).arg(args);

					updateBuilder.call();				
				}

			}
		} catch (AgentError e) {
			this.getLogger().error(e.getLocalizedMessage());
			throw new GradleException(e.getLocalizedMessage());
		} catch (Exception e) {
			this.getLogger().error(e.getLocalizedMessage());
			throw new GradleException(e.getLocalizedMessage());
		}
	}

}
