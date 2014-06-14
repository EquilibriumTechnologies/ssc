package com.eqt.ssc.pig.udf;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.eqt.ssc.model.SSCRecord;
import com.eqt.ssc.serde.RecordBuilder;

//reads in a json string representing the DescribeRegionResult
public class UDFLearning extends EvalFunc<Tuple> {
	TupleFactory tupleF = TupleFactory.getInstance();

	@Override
	public Tuple exec(Tuple tuple) throws IOException {
		String json = tuple.toString();
		System.out.println("JSON: " + json);
		json = json.substring(1,json.length()-1);
		System.out.println("JSON: " + json);
		SSCRecord record = RecordBuilder.read(json);
		tupleF.newTuple(record.value);
		// TODO Auto-generated method stub
		return null;
	}

	public Schema outputSchema(Schema input) {
		try {
//			Schema.FieldSchema describeRegionResultTuple = new Schema.FieldSchema("RegionResult", t)
			Schema.FieldSchema s  = DataType.determineFieldSchema(new DescribeRegionsResult());
			
			return new Schema(s);
//			Schema.FieldSchema tokenFs = new Schema.FieldSchema("token", DataType.CHARARRAY);
//			Schema tupleSchema = new Schema(tokenFs);
//
//			Schema.FieldSchema tupleFs;
//			tupleFs = new Schema.FieldSchema("tuple_of_tokens", tupleSchema, DataType.TUPLE);
//
//			Schema bagSchema = new Schema(tupleFs);
//			bagSchema.setTwoLevelAccessRequired(true);
//			Schema.FieldSchema bagFs = new Schema.FieldSchema("bag_of_tokenTuples", bagSchema, DataType.BAG);
//
//			return new Schema(bagFs);

		} catch (Exception e) {
			return null;
		}
	}

}
