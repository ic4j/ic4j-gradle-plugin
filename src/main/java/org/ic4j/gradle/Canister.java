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

import java.math.BigInteger;

import org.ic4j.candid.parser.IDLArgType;
import org.ic4j.management.Mode;

public class Canister {
	String name;
	
    /**
     * Canister WASM file.
     * 
     */	
	String wasmFile;	
    /**
     * Specifies the id of the canister you want to install or uninstall.  
     * If not specified in install task , new canister will be created.
     * 
     */		
	String canisterId;

    /**
     * Specifies if the canister is deleted in uninstall task.
     * 
     */		
	boolean delete = false;
    		
    /**
	* Force the type of deployment to be reinstall, which overwrites the module. In other
    * words, this erases all data in the canister. By default, upgrade will be chosen
    * automatically if the canister already exists, or install if it does not       
    * [possible values: reinstall]
    * 
    */    		  		
	Mode mode = Mode.upgrade;
	
	/** 
	 *  Specifies the initial cycle balance to deposit into the newly created canister. 
	 *  The specified amount needs to take the canister create fee into account. This amount is
	 *  deducted from the wallet's cycle balance
	 *  
	 */
	BigInteger withCycles;
	
	/**
	 * Specifies the argument to pass to the method
	 * 
	 */	
	String argument;
	
	/**
	 * Specifies the data type for the argument when making the call using an argument
     *      
     * [possible values: idl, raw]
	 */
	IDLArgType argumentType = IDLArgType.idl;	
	
    public Canister(String name) {
        this.name = name;
    }
	

}
