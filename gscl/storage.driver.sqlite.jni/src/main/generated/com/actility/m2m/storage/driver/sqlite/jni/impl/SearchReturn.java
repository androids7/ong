/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.actility.m2m.storage.driver.sqlite.jni.impl;

public class SearchReturn {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected SearchReturn(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SearchReturn obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        sqlitedriverJNI.delete_SearchReturn(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setHasMore(int value) {
    sqlitedriverJNI.SearchReturn_hasMore_set(swigCPtr, this, value);
  }

  public int getHasMore() {
    return sqlitedriverJNI.SearchReturn_hasMore_get(swigCPtr, this);
  }

  public void setData(SWIGTYPE_p_p_subSearchResult value) {
    sqlitedriverJNI.SearchReturn_data_set(swigCPtr, this, SWIGTYPE_p_p_subSearchResult.getCPtr(value));
  }

  public SWIGTYPE_p_p_subSearchResult getData() {
    long cPtr = sqlitedriverJNI.SearchReturn_data_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_p_subSearchResult(cPtr, false);
  }

  public void setSize(int value) {
    sqlitedriverJNI.SearchReturn_size_set(swigCPtr, this, value);
  }

  public int getSize() {
    return sqlitedriverJNI.SearchReturn_size_get(swigCPtr, this);
  }

  public void setAllocated(int value) {
    sqlitedriverJNI.SearchReturn_allocated_set(swigCPtr, this, value);
  }

  public int getAllocated() {
    return sqlitedriverJNI.SearchReturn_allocated_get(swigCPtr, this);
  }

  public void setResult(int value) {
    sqlitedriverJNI.SearchReturn_result_set(swigCPtr, this, value);
  }

  public int getResult() {
    return sqlitedriverJNI.SearchReturn_result_get(swigCPtr, this);
  }

  public SearchReturn() {
    this(sqlitedriverJNI.new_SearchReturn(), true);
  }

}
