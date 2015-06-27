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

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.annotations.Listen;
import com.google.gerrit.reviewdb.client.Branch.NameKey;
import com.google.gerrit.reviewdb.client.PatchSet.Id;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.git.CodeReviewCommit;
import com.google.gerrit.server.git.CommitMergeStatus;
import com.google.gerrit.server.git.validators.MergeValidationException;
import com.google.gerrit.server.git.validators.MergeValidationListener;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lib.Repository;

import java.util.List;

/**
 * <p>
 * The SubmitNoMergeValidationListener fails the validation if the change to be
 * submitted is not a merge commit (&lt;2 parents) and the target branch is
 * configured as a current merge branch in the project settings.
 * </p>
 *
 * <p>
 * If the change is a merge commit then the target branch is removed from the
 * list of merge branches in the project.
 * </p>
 */
@Listen
@Singleton
public class SubmitNoMergeValidationListener implements MergeValidationListener {
  private static final CommitMergeStatus ERROR_CANT_MERGE = CommitMergeStatus.INVALID_PROJECT_CONFIGURATION_PARENT_PROJECT_NOT_FOUND;
  private static final CommitMergeStatus ERROR_INTERNAL_ERROR = CommitMergeStatus.INVALID_PROJECT_CONFIGURATION;

  @Inject
  private com.google.gerrit.server.config.PluginConfigFactory cfg;

  @Override
  public void onPreMerge(Repository repo, CodeReviewCommit commit,
      ProjectState destProject, NameKey destBranch, Id patchSetId)
          throws MergeValidationException {

    PluginConfig pluginConf = getPluginConfiguration(destProject);

    String[] stringList = pluginConf.getStringList(Module.CONFIG_MERGEBRANCHES);
    if (stringList == null) {
      return;
    }

    List<String> mergeBranches = Lists.newArrayList(stringList);
    String destBranchName = destBranch.getShortName();
    if (mergeBranches.contains(destBranchName)) {
      if (commit.getParentCount() <= 1) {
        throw new MergeValidationException(ERROR_INTERNAL_ERROR);
      } else {
        // TODO: this should happen after (instead of before) the merge
        mergeBranches.remove(destBranchName);
        pluginConf.setStringList(Module.CONFIG_MERGEBRANCHES, mergeBranches);
      }
    }
  }

  private PluginConfig getPluginConfiguration(ProjectState destProject)
      throws MergeValidationException {

    try {
      return cfg.getFromProjectConfig(destProject.getProject().getNameKey(), Module.PLUGIN_NAME);
    } catch (NoSuchProjectException e) {
      throw new MergeValidationException(ERROR_CANT_MERGE);
    }
  }
}
