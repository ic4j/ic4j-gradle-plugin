# IC4J Gradle Plugin


The IC4J Gradle Plugin allows for the installation and uninstallation of ICP canisters from Gradle scripts.

To install or uninstall an ICP canister, add the install Gradle plugin to your Gradle script.

run 
```gradle install ```
to install canister

or ```gradle uninstall``` to uninstall canister

or ```gradle call``` to call canister

```
plugins {
	id 'org.ic4j.ic4j-gradle-plugin'
}


install {
		network = 'http://localhost:4943/'
		identity = '/Users/roman/.config/dfx/identity/default/identity.pem'	
		isLocal = true			
		
		canisters{
			canister1
			{
				mode= 'install'
		        wasmFile ='hello.wasm'
		        argument='("from Gradle")'
			}
		}
	
}

uninstall {

		network = 'http://localhost:4943/'
		identity = '/Users/roman/.config/dfx/identity/default/identity.pem'	
		isLocal = true			
		
		canisters{
			canister1
			{
		        canisterId = 'yahli-baaaa-aaaaa-aabtq-cai'
		        delete = 'true'
			}
		}
	
}

call {

		network = 'http://localhost:4943/'
		identity = '/Users/roman/.config/dfx/identity/default/identity.pem'		
		isLocal = true
		
		canisters{
			canister1
			{
		        canisterId = 'yhgn4-myaaa-aaaaa-aabta-cai'
		        method ='greet'
		        argument='("Gradle")'
			}
			
			canister2
			{
		        canisterId = 'yhgn4-myaaa-aaaaa-aabta-cai'
		        method ='peek'
		        methodType='QUERY'
			}			
		}
}
```

