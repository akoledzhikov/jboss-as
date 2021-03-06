/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.ejb.timerservice.entity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests bean managed persistence
 */
@RunWith(Arquillian.class)
public class BMPEntityBeanTimerTestCase {

    private static final String ARCHIVE_NAME = "SimpleLocalHomeTest.war";

    @ArquillianResource
    private InitialContext iniCtx;

    @Deployment
    public static Archive<?> deploy() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME);
        war.addPackage(BMPEntityBeanTimerTestCase.class.getPackage());
        war.addAsWebInfResource(BMPEntityBeanTimerTestCase.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml");
        return war;
    }

    @Test
    public void testEntityBeanTimerService() throws Exception {
        DataStore.DATA.clear();
        DataStore.DATA.put(20, "Existing");
        BMPLocalHome home = getHome();
        BMPLocalInterface ejbInstance = home.createWithValue("Hello");
        Integer createdPk = (Integer) ejbInstance.getPrimaryKey();

        ejbInstance.setupTimer();
        ejbInstance = home.findByPrimaryKey(20);
        ejbInstance.setupTimer();
        SimpleBMPBean.getLatch().await(10, TimeUnit.SECONDS);
        Map<Integer, String> data = SimpleBMPBean.getTimerData();

        Assert.assertTrue(data.containsKey(createdPk));
        Assert.assertTrue(data.containsKey(20));
        Assert.assertEquals("Hello", data.get(createdPk));
        Assert.assertEquals("Existing", data.get(20));
    }

    private BMPLocalHome getHome() throws NamingException {
        return (BMPLocalHome) iniCtx.lookup("java:module/SimpleBMP!" + BMPLocalHome.class.getName());
    }
}
