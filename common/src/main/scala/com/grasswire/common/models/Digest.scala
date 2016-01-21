package com.grasswire.common.models

import com.grasswire.common.Username
import com.grasswire.common.json_models.StoryJsonModel

/**
 * Created by levinotik on 6/30/15.
 */
case class Digest(createdBy: Username, stories: List[StoryJsonModel])
