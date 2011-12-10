package org.mca.data.dmatrix;

import org.mca.data.DataPartInfo;


public class SubMatrixInfo extends DataPartInfo {

	private static final long serialVersionUID = 1L;

	
	/** Width of the submatrix */
	public Integer width;

	/** Height of the submatrix */
	public Integer height;
	
	public SubMatrixInfo() {}

	public SubMatrixInfo(Integer width, Integer height, String name,
			Integer part) {
		super(name, part);
		this.width = width;
		this.height = height;
	}
	
	public SubMatrixInfo(Integer height, Integer width) {
		this.width = width;
		this.height = height;
	}

	@Override
	public String toString() {
		return "SubMatrixInfo [name=" + name + ", part=" + part + ", width="
				+ width + ", height=" + height + "]";
	}
	
}
