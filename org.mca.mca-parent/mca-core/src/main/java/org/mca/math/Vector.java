package org.mca.math;

import org.mca.log.LogUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

@SuppressWarnings("serial")
public class Vector<E> extends Data<E> {

	public Integer size;
	public Integer nbPart;

	public E get(int index) throws Exception{
		if (index >= size ) throw new Exception();
		int partSize = size / nbPart;
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = (SubVector<E>)dataParts.get(num);
		return vector.get(indexLocal);
	}
	
	public void set(int index, E value) throws Exception{
		if (index >= size ) throw new Exception();
		int partSize = size / nbPart;
		int num = index / partSize;
		int indexLocal = index - (num * partSize)  ;
		LogUtil.debug("l'élément [" + index + "] se trouve dans la partie [" + num + "] à l'index [" + indexLocal + "]", getClass());
		SubVector<E> vector = (SubVector<E>)dataParts.get(num);
		vector.set(indexLocal, value);
	}

	@Override
	protected void storeProperties(Element node) {
		node.setAttribute("size", this.size.toString());
		node.setAttribute("nbPart", this.nbPart.toString());
	}
	
	@Override
	protected void parseProperties(NamedNodeMap attributes) {
		size = Integer.valueOf(attributes.getNamedItem("size").getNodeValue());
		nbPart = Integer.valueOf(attributes.getNamedItem("nbPart").getNodeValue());
	}


}
