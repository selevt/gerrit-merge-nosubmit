// Copyright (C) 2015 Stefan Schmitt
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.selevt.gerrit.plugins.mergenosubmit;

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.config.ProjectConfigEntry;
import com.google.gerrit.server.config.ProjectConfigEntry.Type;
import com.google.gerrit.server.git.validators.MergeValidationListener;
import com.google.inject.AbstractModule;

/**
 * Module configuration for the submit no-merge plugin.
 */
class Module extends AbstractModule {
  public static final String CONFIG_MERGEBRANCHES = "mergebranches";
  public static final String PLUGIN_NAME = "restrictsubmit";

  @Override
  protected void configure() {
    DynamicSet.bind(binder(), MergeValidationListener.class)
        .to(SubmitNoMergeValidationListener.class);

    String mergeBranchesDisplayName = "Merge Branches";
    String mergeBranchesDescription = "Changes on these branches cannot be submitted, "
        + "until a merge branch on the respective branch has been submitted";

    ProjectConfigEntry configEntry =
        new ProjectConfigEntry(mergeBranchesDisplayName, null, Type.ARRAY, null,
            false, mergeBranchesDescription);

    bind(ProjectConfigEntry.class)
       .annotatedWith(Exports.named(CONFIG_MERGEBRANCHES))
       .toInstance(configEntry);
  }
}
