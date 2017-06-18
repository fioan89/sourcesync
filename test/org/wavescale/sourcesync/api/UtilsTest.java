package org.wavescale.sourcesync.api;

import com.intellij.mock.MockProject;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ****************************************************************************
 * Copyright (c) 2014-2107 Faur Ioan-Aurel.                                     *
 * All rights reserved. This program and the accompanying materials             *
 * are made available under the terms of the MIT License                        *
 * which accompanies this distribution, and is available at                     *
 * http://opensource.org/licenses/MIT                                           *
 * *
 * For any issues or questions send an email at: fioan89@gmail.com              *
 * *****************************************************************************
 */
@RunWith(DataProviderRunner.class)
public class UtilsTest {
    private static final String WITH_MODULE_RELATIVE_PATH = "module/src/org/sourcesync/test";
    private static final String WITHOUT_MODULE_RELATIVE_PATH = "src/org/sourcesync/test";

    private static final MockVirtualFile rootDirectory = new MockVirtualFile(true, "/");
    private static final MockVirtualFile homeDirectory = new MockVirtualFile(true, "home");
    private static final MockVirtualFile studioProjectsDirectory = new MockVirtualFile(true, "StudioProjects");
    private static final MockVirtualFile projectDirectory = new MockVirtualFile(true, "MyTestProject");

    private MockProject project;

    @DataProvider
    public static Object[][] dataProviderDirsToFileFromProjectRoot() {
        final MockVirtualFile javaFile = new MockVirtualFile(true, "MyJavaFile.java");
        javaFile.setParent(getVFDirectoryWithModule());

        final MockVirtualFile projectFile = new MockVirtualFile(true, "projectFile.txt");
        projectFile.setParent(projectDirectory);

        final MockVirtualFile withoutModule = new MockVirtualFile(true, "MyJava2File.java");
        withoutModule.setParent(getVFDirectoryWithoutModule());


        return new Object[][]{
                {Paths.get(WITH_MODULE_RELATIVE_PATH), javaFile},
                {Paths.get(""), projectFile},
                {Paths.get(WITHOUT_MODULE_RELATIVE_PATH), withoutModule},
        };
    }

    private static VirtualFile getVFDirectoryWithModule() {
        final MockVirtualFile moduleDirectory = new MockVirtualFile(true, "module");
        final MockVirtualFile srcDirectory = new MockVirtualFile(true, "src");
        final MockVirtualFile orgPackageDirectory = new MockVirtualFile(true, "org");
        final MockVirtualFile sourceSyncPackageDirectory = new MockVirtualFile(true, "sourcesync");
        final MockVirtualFile testPackageDirectory = new MockVirtualFile(true, "test");

        moduleDirectory.setParent(projectDirectory);
        srcDirectory.setParent(moduleDirectory);
        orgPackageDirectory.setParent(srcDirectory);
        sourceSyncPackageDirectory.setParent(orgPackageDirectory);
        testPackageDirectory.setParent(sourceSyncPackageDirectory);
        return testPackageDirectory;
    }

    private static VirtualFile getVFDirectoryWithoutModule() {
        final MockVirtualFile srcDirectory = new MockVirtualFile(true, "src");
        final MockVirtualFile orgPackageDirectory = new MockVirtualFile(true, "org");
        final MockVirtualFile sourceSyncPackageDirectory = new MockVirtualFile(true, "sourcesync");
        final MockVirtualFile testPackageDirectory = new MockVirtualFile(true, "test");

        srcDirectory.setParent(projectDirectory);
        orgPackageDirectory.setParent(srcDirectory);
        sourceSyncPackageDirectory.setParent(orgPackageDirectory);
        testPackageDirectory.setParent(sourceSyncPackageDirectory);
        return testPackageDirectory;
    }

    @Before
    public void setUp() throws Exception {
        homeDirectory.setParent(rootDirectory);
        studioProjectsDirectory.setParent(homeDirectory);
        projectDirectory.setParent(studioProjectsDirectory);

        project = mock(MockProject.class);
        when(project.getBaseDir()).thenReturn(projectDirectory);
    }

    @Test
    @UseDataProvider("dataProviderDirsToFileFromProjectRoot")
    public void dirsToFileFromProjectRoot(Path expected, VirtualFile vFile) throws Exception {
        Path actualPath = Utils.dirsToFileFromProjectRoot(vFile, project);
        assertEquals(expected, actualPath);
    }

}