package com.eqt.ssc.pig.udf;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

//reads in a json string representing the DescribeRegionResult
public class UDFLearning extends EvalFunc<DataBag> {

	@Override
	public DataBag exec(Tuple tuple) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Schema outputSchema(Schema input) {
		try {
			Schema.FieldSchema tokenFs = new Schema.FieldSchema("token", DataType.CHARARRAY);
			Schema tupleSchema = new Schema(tokenFs);

			Schema.FieldSchema tupleFs;
			tupleFs = new Schema.FieldSchema("tuple_of_tokens", tupleSchema, DataType.TUPLE);

			Schema bagSchema = new Schema(tupleFs);
			bagSchema.setTwoLevelAccessRequired(true);
			Schema.FieldSchema bagFs = new Schema.FieldSchema("bag_of_tokenTuples", bagSchema, DataType.BAG);

			return new Schema(bagFs);

		} catch (Exception e) {
			return null;
		}
	}

}
