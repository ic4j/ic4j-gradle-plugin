# IC4J Gradle Plugin


The IC4J Gradle Plugin allows for the installation and uninstallation of ICP canisters from Gradle scripts.

To install or uninstall an ICP canister, add the install Gradle plugin to your Gradle script.

run 
```gradle install ```
to install

or ```gradle uninstall``` to uninstall

```
plugins {
	id 'org.ic4j.ic4j-gradle-plugin'
}


install {
		network = 'http://localhost:4943/'
		identity = 'identity.pem'		
		
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
		identity = 'identity.pem'		
		
		canisters{
			canister1
			{
		        canisterId = 'udtxk-viaaa-aaaaa-aaa6a-cai'
		        delete = 'false'
			}
		}	
}
```

