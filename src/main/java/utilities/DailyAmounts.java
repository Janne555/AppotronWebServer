/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

/**
 *
 * @author Janne
 */
public enum DailyAmounts {
    IRON(9), SODIUM(2000), POTASSIUM(3500), CALCIUM(800), VITB12(2), VITC(75), VITD(10);
    private final float value;

    private DailyAmounts(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
    
}
