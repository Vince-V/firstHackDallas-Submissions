package com.verizon.ssp.roctool.object;

public class Signature {
	private String signature;
	
	Signature() {}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
		return "Signature [signature=" + signature + "]";
	}
	
}
