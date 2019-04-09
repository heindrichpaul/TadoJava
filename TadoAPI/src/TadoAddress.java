public class TadoAddress {
	private String addressLine1;
	private String addressLine2;
	private String zipCode;
	private String city;
	private String state;
	private String country;

	public String getAddressLine1() {
		return addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

	public TadoAddress(String addressLine1, String addressLine2, String zipCode, String city, String state,
			String country) {
		super();
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.zipCode = zipCode;
		this.city = city;
		this.state = state;
		this.country = country;
	}
}