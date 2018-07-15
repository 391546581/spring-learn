package com.jay.spring.context.annotation;

import com.jay.spring.Exception.BeanDefinitionException;
import com.jay.spring.bean.BeanDefinition;
import com.jay.spring.bean.factory.BeanDefinitionStoreException;
import com.jay.spring.bean.factory.support.BeanDefinitionRegistry;
import com.jay.spring.bean.factory.support.BeanNameGenerator;
import com.jay.spring.core.io.Resource;
import com.jay.spring.core.io.support.PackageResourceLoader;
import com.jay.spring.core.type.classreading.MetadataReader;
import com.jay.spring.core.type.classreading.SimpleMetadataReader;
import com.jay.spring.stereotype.Component;
import com.jay.spring.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by xiang.wei on 2018/7/15
 *
 * @author xiang.wei
 */
public class ClassPathBeanDefinitionScanner {
    private final BeanDefinitionRegistry registry;

    private PackageResourceLoader resourceLoader = new PackageResourceLoader();

    private final Log logger = LogFactory.getLog(getClass());

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public Set<BeanDefinition> doScan(String packagesToScan) {
        String[] basePackages = StringUtils.tokenizeToStringArray(packagesToScan, ",");

        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<BeanDefinition>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
            for (BeanDefinition candidate : candidates) {
                beanDefinitions.add(candidate);
                registry.registerBeanDefinition(candidate.getID(), candidate);
            }
        }
        return beanDefinitions;
    }

    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
        try {
            Resource[] resources = this.resourceLoader.getResources(basePackage);
            for (Resource resource : resources) {
                try {
                    MetadataReader metadataReader = new SimpleMetadataReader(resource);
                    if (metadataReader.getAnnotationMetadata().hasAnnotation(Component.class.getName())) {
                        ScannedGenericBeanDefinition sbd=new ScannedGenericBeanDefinition(metadataReader.getAnnotationMetadata());
                        String beanName = this.beanNameGenerator.generateBeanName(sbd, this.registry);
                        sbd.setId(beanName);
                        candidates.add(sbd);
                    }
                } catch (Throwable ex) {
                    throw new BeanDefinitionStoreException("Failed to read candidate component class:" + resource, ex
                    );
                }


            }


        } catch (IOException e) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", e);
        }
        return candidates;
    }

}
