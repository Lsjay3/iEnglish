/**
 * 
 */
package com.example.ienglish.IseResult;

/**
 * <p>Title: FinalResult</p>
 * <p>Description: </p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date 2020年5月23日 下午8:17:58
 */
public class FinalResult extends Result {
	
	public int ret;
	
	public float total_score;
	
	@Override
	public String toString() {
		return "返回值：" + ret + "，总分：" + total_score;
	}
}
