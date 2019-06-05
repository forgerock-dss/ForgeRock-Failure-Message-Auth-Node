/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */


package com.example.FailureMessageAuthNode;

import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.FAILURE_URL;


/**
 * Node which display custom failure error message
 */


@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = FailureMessageAuthNode.Config.class)


public class FailureMessageAuthNode extends SingleOutcomeNode {

    private final Logger logger = LoggerFactory.getLogger(FailureMessageAuthNode.class);
    private final Config config;
    private final Realm realm;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * Set mode get failure from SharedState or from FailureMessage parameter
         */
        @Attribute(order = 100, validators = {RequiredValueValidator.class})
        default boolean UseSharedState() {
            return false;
        }

        /**
         * Shared State attribute name
         */
        @Attribute(order = 200)
        String SharedStateAttribute();

        /**
         * Failure message attribute
         */
        @Attribute(order = 300)
        String FailureMessage();
    }


    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm  The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public FailureMessageAuthNode(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        JsonValue newState = context.sharedState.copy();

        if (config.UseSharedState()) {

            logger.debug("Failure Message Node true path started");

            //For reference can use JsonValue too as below
            //JsonValue FailureSharedStateValue = context.sharedState.get(config.SharedStateAttribute());

            String FailureSharedStateValue = context.sharedState.get(config.SharedStateAttribute()).asString();

            logger.debug("Shared state attribute name set to {}", config.SharedStateAttribute());
            logger.debug("Shared state value is {}",FailureSharedStateValue);

            newState.put(FAILURE_URL, config.SharedStateAttribute());
            return goToNext().replaceSharedState(context.sharedState.copy().put(FAILURE_URL,FailureSharedStateValue)).build();

        }

        else {

                logger.debug("Failure Message Node false path started");
                logger.debug("Failure message set to {}", config.FailureMessage());

                newState.put(FAILURE_URL, config.FailureMessage());
                return goToNext().replaceSharedState(context.sharedState.copy().put(FAILURE_URL, config.FailureMessage())).build();

        }

    }

}
