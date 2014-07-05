package com.eqt.ssc.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.SSCRecord;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.serde.RecordBuilder;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;

/**
 * base class for collecting against a particular api call to check to see if
 * it has changed from the last time it was called.
 * @author gman
 *
 */
public abstract class APICollector {
	
	private Log LOG = LogFactory.getLog(APICollector.class);

	//used for writting out logs
	protected StateEngine state;
	
	protected static final String PROP_DEFAULT_INTERVAL = "ssc.account.check.interval.default.seconds";
	
	//number that can be used to track the number of api calls that changed.
	protected int stateChanges = 0;
	
	public APICollector(StateEngine state) {
		this.state = state;
	}
	
	public int getStateChanges() {
		return stateChanges;
	}
	
	/**
	 * implement each state check in here.
	 * @return SSCAccountStatus with the updated token and any other stats to record
	 */
	public abstract SSCAccountStatus collect(Token token);
	
	/**
	 * use this to grab the interval frequency in which we check on.
	 */
	public int getIntervalTime() {
		return Props.getPropInt(getCustomIntervalProperty(),PROP_DEFAULT_INTERVAL,"57")*1000;
	}
	
	/**
	 * simple method to return a unique name
	 * @return
	 */
	public String getCollectorName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * this is called to get the overriding property from the props file
	 * @return
	 */
	protected abstract String getCustomIntervalProperty();
	
	/**
	 * Used to do a simple object.equals(lastObject) test.
	 * Simply call it like this: compareObjects(ec2.describeTags(), "ec2.DescribeTags")
	 * Internally this will also update the stateChanged count.
	 * @param object result of the AWS API method call
	 * @param methodName english version of the method just called
	 * @return true if the state was changed
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Object> boolean compareObjects(T object, String methodName, String accoundId) {
		SSCRecord record = RecordBuilder.createRecord(object, methodName, accoundId);
		String lastKnown = state.getLastKnown(record.key);
		
		//never seen before, must be new!
		if(lastKnown == null) {
			LOG.debug("new Record: " + record.key.toString());
			state.writeNewRecord(record);
			stateChanges += 1;
			return true;		
		}
		
//		LOG.debug("LAST RECORD: " + (lastKnown.length() > 500?lastKnown.substring(0,500)+"...":lastKnown));
		
		SSCRecord last = RecordBuilder.read(lastKnown);
		T value =  (T) last.value;
		if(!object.equals(value)) {
			LOG.debug("new Record: " + record.key.toString());
			state.writeNewRecord(record);
			stateChanges += 1;
			return true;
		} else {
			LOG.debug(methodName + " no state change");
			return false;
		}
	}
	
	/**
	 * Same as compareObjects except that it does the matching at the string level.
	 * Not all objects implement a correct equals method. Anything using the aws class
	 * com.amazonaws.internal.ListWithAutoConstructFlag appears broken.
	 * Internally this will also update the stateChanged count.
	 * @param o
	 * @param methodName
	 * @return
	 */
	protected boolean compareJson(Object o, String methodName, String accoundId) {
		SSCRecord record = RecordBuilder.createRecord(o, methodName, accoundId);
		String lastKnown = state.getLastKnown(record.key);

		//never seen before, must be new!
		if(lastKnown == null) {
			LOG.debug("new Record: " + record.key.toString());
			state.writeNewRecord(record);
			stateChanges += 1;
			return true;		
		}
		
		//compare values at the string level
		String newJsonValue  = RecordBuilder.serialize(record.value);
		String oldJsonValue = RecordBuilder.valueAsJson(lastKnown);
//		System.out.println("OLD: " + oldJsonValue);
//		System.out.println("NEW: " + newJsonValue);
		
		//still buggy, returns false when huge json's are identical via a string.equals()
//		if(RecordBuilder.equality(newJsonValue, oldJsonValue)) {
		if(!newJsonValue.equals(oldJsonValue)) {
			LOG.debug("new Record: " + record.key.toString());
			state.writeNewRecord(record);
			stateChanges += 1;
			return true;
		} else {
			LOG.debug(methodName + " no state change");
			return false;
		}
	}
}
