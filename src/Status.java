public enum Status {
	ACTIVE {
		@Override
		public String toString() {
			return "Active";
		}
	},
	INACTIVE {
		@Override
		public String toString() {
			return "Inactive";
		}
	}
}
