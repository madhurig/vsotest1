// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.ui.checkout;

import com.microsoft.alm.plugin.idea.IdeaAbstractTest;
import com.microsoft.alm.plugin.idea.ui.common.mocks.MockObserver;
import org.junit.Assert;
import org.junit.Test;

public class CheckoutModelTest extends IdeaAbstractTest {

    /**
     * This is just a really basic test of the constructor(s)
     * It checks all the variants of the constructor(s)
     * It checks the values of the properties right after construction
     */
    @Test
    public void testConstructor() {
        CheckoutModel cm = new CheckoutModel(null, null);
        Assert.assertTrue(cm.getTfsModel() != null);
        Assert.assertTrue(cm.getVsoModel() != null);
    }

    /**
     * This test makes sure that all setters on the page model notify the observer if and only if the value changes.
     */
    @Test
    public void testObservable() {
        CheckoutModel cm = new CheckoutModel(null, null);
        MockObserver observer = new MockObserver(cm);

        // Change vso selected and make sure that we get notified
        // The default value is true, so we set it here to false
        cm.setVsoSelected(false);
        observer.assertAndClearLastUpdate(cm, CheckoutModel.PROP_VSO_SELECTED);
        Assert.assertEquals(false, cm.isVsoSelected());
        // Set connected to the exact same value and make sure we don't get notified
        cm.setVsoSelected(false);
        observer.assertAndClearLastUpdate(null, null);
        Assert.assertEquals(false, cm.isVsoSelected());

        // Change clone enabled for Vso and make sure that we get notified
        cm.setCloneEnabledForVso(true);
        observer.assertAndClearLastUpdate(cm, CheckoutModel.PROP_CLONE_ENABLED);

        // Set clone enabled to the exact same value and make sure we don't get notified
        cm.setCloneEnabledForVso(true);
        observer.assertAndClearLastUpdate(null, null);

        //change clone enabled for Tfs and make sure that we get notified
        cm.setCloneEnabledForTfs(true);
        observer.assertAndClearLastUpdate(cm, CheckoutModel.PROP_CLONE_ENABLED);

        // Set clone enabled to the exact same value and make sure we don't get notified
        cm.setCloneEnabledForTfs(true);
        observer.assertAndClearLastUpdate(null, null);
    }

}